/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.folio.inventory.domain.instances;

import io.vertx.core.json.JsonObject;

/**
 *
 * @author ne
 */
public class InstanceRelationship {
  // JSON property names
  public static final String SUPER_INSTANCE_ID_KEY = "superInstanceId";
  public static final String SUB_INSTANCE_ID_KEY = "subInstanceId";
  public static final String INSTANCE_RELATIONSHIP_TYPE_ID_KEY = "instanceRelationshipTypeId";

  public final String id;
  public final String superInstanceId;
  public final String subInstanceId;
  public final String instanceRelationshipTypeId;

  public InstanceRelationship (String id, String superInstanceId, String subInstanceId, String instanceRelationshipTypeId) {
    this.id = id;
    this.superInstanceId = superInstanceId;
    this.subInstanceId = subInstanceId;
    this.instanceRelationshipTypeId = instanceRelationshipTypeId;
  }

  public InstanceRelationship (JsonObject rel) {
    this(rel.getString("id"),
         rel.getString(SUPER_INSTANCE_ID_KEY),
         rel.getString(SUB_INSTANCE_ID_KEY),
         rel.getString(INSTANCE_RELATIONSHIP_TYPE_ID_KEY));
  }

  @Override
  public String toString() {
    return "{ \"id\": \"" + id + "\", \"superInstanceId\": \""+ superInstanceId + "\", \"subInstanceId\": \""+ subInstanceId + "\", \"instanceRelationshipTypeId\": \"" + instanceRelationshipTypeId + "\" }";
  }

  @Override
  public boolean equals (Object object) {
    if (object instanceof InstanceRelationship) {
      return ((InstanceRelationship) object).toString().equals(this.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
