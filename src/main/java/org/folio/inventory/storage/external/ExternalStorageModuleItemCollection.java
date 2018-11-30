package org.folio.inventory.storage.external;

import static org.folio.inventory.support.JsonArrayHelper.toList;
import static org.folio.inventory.support.JsonHelper.getNestedProperty;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.folio.inventory.domain.items.Item;
import org.folio.inventory.domain.items.ItemCollection;
import org.folio.inventory.domain.items.Note;
import org.folio.inventory.support.JsonArrayHelper;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

class ExternalStorageModuleItemCollection
  extends ExternalStorageModuleCollection<Item>
  implements ItemCollection {

  ExternalStorageModuleItemCollection(Vertx vertx,
                                      String baseAddress,
                                      String tenant,
                                      String token) {

    super(vertx, String.format("%s/%s", baseAddress, "item-storage/items"),
      tenant, token, "items");
  }

  @Override
  protected Item mapFromJson(JsonObject itemFromServer) {

    List<String> formerIds = JsonArrayHelper.toListOfStrings(itemFromServer.getJsonArray(Item.FORMER_IDS_KEY));
    List<String> copyNumberList = JsonArrayHelper.toListOfStrings(itemFromServer.getJsonArray("copyNumbers"));

    List<JsonObject> notes = toList(
      itemFromServer.getJsonArray(Item.NOTES_KEY, new JsonArray()));

    List<Note> mappedNotes = notes.stream()
      .map(it -> new Note(it))
      .collect(Collectors.toList());

    return new Item(
      itemFromServer.getString("id"),
      itemFromServer.getString("holdingsRecordId"),
      getNestedProperty(itemFromServer, "status", "name"),
      itemFromServer.getString("materialTypeId"),
      itemFromServer.getString("permanentLoanTypeId"),
      itemFromServer.getJsonObject("metadata"))
            .setHrid(itemFromServer.getString(Item.HRID_KEY))
            .setFormerIds(formerIds)
            .setDiscoverySuppress(itemFromServer.getBoolean(Item.DISCOVERY_SUPPRESS_KEY))
            .setBarcode(itemFromServer.getString("barcode"))
            .setItemLevelCallNumber(itemFromServer.getString(Item.ITEM_LEVEL_CALL_NUMBER_KEY))
            .setItemLevelCallNumberPrefix(itemFromServer.getString(Item.ITEM_LEVEL_CALL_NUMBER_PREFIX_KEY))
            .setItemLevelCallNumberSuffix(itemFromServer.getString(Item.ITEM_LEVEL_CALL_NUMBER_SUFFIX_KEY))
            .setItemLevelCallNumberTypeId(itemFromServer.getString(Item.ITEM_LEVEL_CALL_NUMBER_TYPE_ID_KEY))
            .setVolume(itemFromServer.getString(Item.VOLUME_KEY))
            .setEnumeration(itemFromServer.getString("enumeration"))
            .setChronology(itemFromServer.getString("chronology"))
            .setCopyNumbers(copyNumberList)
            .setNumberOfPieces(itemFromServer.getString("numberOfPieces"))
            .setNotes(mappedNotes)
            .setPermanentLocationId(itemFromServer.getString("permanentLocationId"))
            .setTemporaryLocationId(itemFromServer.getString("temporaryLocationId"))
            .setTemporaryLoanTypeId(itemFromServer.getString("temporaryLoanTypeId"));
  }

  @Override
  protected String getId(Item record) {
    return record.id;
  }

  @Override
  protected JsonObject mapToRequest(Item item) {
    JsonObject itemToSend = new JsonObject();

    //TODO: Review if this shouldn't be defaulting here
    itemToSend.put("id", item.id != null
      ? item.id
      : UUID.randomUUID().toString());

    if(item.getStatus() != null) {
      itemToSend.put("status", new JsonObject().put("name", item.getStatus()));
    }

    includeIfPresent(itemToSend, Item.HRID_KEY, item.getHrid());
    itemToSend.put(Item.FORMER_IDS_KEY, item.getFormerIds());
    itemToSend.put(Item.DISCOVERY_SUPPRESS_KEY, item.getDiscoverySuppress());
    itemToSend.put("copyNumbers", item.getCopyNumbers());
    itemToSend.put("notes", item.getNotes());
    includeIfPresent(itemToSend, "barcode", item.getBarcode());
    includeIfPresent(itemToSend, Item.ITEM_LEVEL_CALL_NUMBER_KEY, item.getItemLevelCallNumber());
    includeIfPresent(itemToSend, Item.ITEM_LEVEL_CALL_NUMBER_PREFIX_KEY, item.getItemLevelCallNumberPrefix());
    includeIfPresent(itemToSend, Item.ITEM_LEVEL_CALL_NUMBER_SUFFIX_KEY, item.getItemLevelCallNumberSuffix());
    includeIfPresent(itemToSend, Item.ITEM_LEVEL_CALL_NUMBER_TYPE_ID_KEY, item.getItemLevelCallNumberTypeId());
    includeIfPresent(itemToSend, Item.VOLUME_KEY, item.getVolume());
    includeIfPresent(itemToSend, "enumeration", item.getEnumeration());
    includeIfPresent(itemToSend, "chronology", item.getChronology());
    includeIfPresent(itemToSend, "numberOfPieces", item.getNumberOfPieces());
    includeIfPresent(itemToSend, "holdingsRecordId", item.getHoldingId());
    includeIfPresent(itemToSend, "materialTypeId", item.getMaterialTypeId());
    includeIfPresent(itemToSend, "permanentLoanTypeId", item.getPermanentLoanTypeId());
    includeIfPresent(itemToSend, "temporaryLoanTypeId", item.getTemporaryLoanTypeId());
    includeIfPresent(itemToSend, "permanentLocationId", item.getPermanentLocationId());
    includeIfPresent(itemToSend, "temporaryLocationId", item.getTemporaryLocationId());

    return itemToSend;
  }
}
