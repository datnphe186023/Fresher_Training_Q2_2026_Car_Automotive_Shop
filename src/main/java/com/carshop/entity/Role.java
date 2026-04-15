package com.carshop.entity;

/**
 * Enum representing user roles in the system.
 * Only ADMIN and STAFF roles are supported for internal system users.
 */
public enum Role {
    /**
     * Administrator role with full system access
     */
    ADMIN,
    
    /**
     * Staff role with limited management access
     */
    STAFF
}
