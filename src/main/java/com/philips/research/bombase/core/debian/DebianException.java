/*
 * Copyleft (c) 2024, Alexandre Beaurain
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.debian;

import com.philips.research.bombase.core.BusinessException;

public class DebianException extends BusinessException {
    public DebianException(String message) {
        super(message);
    }

    public DebianException(String message, Throwable cause) {
        super(message, cause);
    }
}
