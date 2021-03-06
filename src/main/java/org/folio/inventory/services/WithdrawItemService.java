package org.folio.inventory.services;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.folio.inventory.domain.items.ItemStatusName.WITHDRAWN;
import static org.folio.inventory.domain.view.request.RequestStatus.OPEN_NOT_YET_FILLED;

import java.util.concurrent.CompletableFuture;

import org.folio.inventory.common.WebContext;
import org.folio.inventory.domain.items.Item;
import org.folio.inventory.domain.items.ItemCollection;
import org.folio.inventory.domain.view.request.Request;
import org.folio.inventory.storage.external.Clients;
import org.folio.inventory.storage.external.repository.RequestRepository;
import org.folio.inventory.validation.ItemMarkAsWithdrawnValidators;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WithdrawItemService {
  private static final Logger log = LoggerFactory.getLogger(WithdrawItemService.class);

  private final ItemCollection itemCollection;
  private final RequestRepository requestRepository;

  public WithdrawItemService(ItemCollection itemCollection, Clients clients) {
    this.itemCollection = itemCollection;
    this.requestRepository = new RequestRepository(clients);
  }

  public CompletableFuture<Item> processMarkItemWithdrawn(WebContext context) {
    final String itemId = context.getStringParameter("id", null);

    return itemCollection.findById(itemId)
      .thenCompose(ItemMarkAsWithdrawnValidators::itemIsFound)
      .thenCompose(ItemMarkAsWithdrawnValidators::itemHasAllowedStatusToMarkAsWithdrawn)
      .thenCompose(this::updateRequestStatusIfRequired)
      .thenApply(item -> item.changeStatus(WITHDRAWN))
      .thenCompose(itemCollection::update);
  }

  private CompletableFuture<Item> updateRequestStatusIfRequired(Item item) {
    return requestRepository.getRequestInFulfilmentForItem(item.id)
      .thenCompose(requestOptional -> {
        if (!requestOptional.isPresent() || requestIsExpiredOnHoldShelf(requestOptional.get())) {
          log.debug("No request in fulfillment or it is expired");
          return completedFuture(item);
        }

        log.debug("Fount request in fulfillment {}", requestOptional.get().getId());
        return moveRequestIntoNotYetFilledStatus(requestOptional.get())
          .thenApply(notUsed -> item);
      });
  }

  private boolean requestIsExpiredOnHoldShelf(Request request) {
    return request.getHoldShelfExpirationDate() != null
      && currentDateTime().isAfter(request.getHoldShelfExpirationDate());
  }

  private CompletableFuture<Request> moveRequestIntoNotYetFilledStatus(Request request) {
    request.setStatus(OPEN_NOT_YET_FILLED);

    return requestRepository.update(request);
  }

  private DateTime currentDateTime() {
    return DateTime.now(DateTimeZone.UTC);
  }
}
