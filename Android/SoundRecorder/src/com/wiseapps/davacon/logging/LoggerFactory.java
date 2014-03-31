package com.wiseapps.davacon.logging;

/**
 * Copyright 2013 Pozitron.
 *
 * All rights reserved.
 * POZITRON PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 * @author varya.bzhezinskaya@gmail.pozitron
 * Date: 11/13/12
 * Time: 4:10 AM
 */
public class LoggerFactory {

    public static Logger obtainLogger(final String tag) {
        return new MultipleLoggersSupport(new AndroidLogger(tag));
    }
}
