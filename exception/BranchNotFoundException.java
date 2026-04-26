package com.library.exception;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(String branchId) {
        super("Library branch not found with ID: " + branchId);
    }
}
