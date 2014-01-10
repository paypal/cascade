package com.paypal.stingray.common.db;

/**
 * For use with the IndexedField annotation. Insert - only write fields with
 * this annotation value on Insert InsertAndUpdate - write fields with this
 * annotation value on both Insert and Update
 * 
 * @author Will
 * 
 */

public enum WriteOn {
  Insert, InsertAndUpdate
}
