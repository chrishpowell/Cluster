/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.function.Function;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public interface CheckedFunction<T, R> extends Function<T, R>
{
    @Override
    default R apply(T t) 
    {
        try
            { return applyAndThrow(t); }
        catch (Exception e)
            { throw new RuntimeException(e); }
    }

    R applyAndThrow(T t) throws Exception;
}
