/**
 * Copyright (c) 2017 Ivan Magda
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ivanmagda.network.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URL;

/**
 * Helper wrapper class around necessary parameters for executing a network query.
 *
 * @param <A> The result type, after the invoking a network query.
 */
public final class Resource<A> {

    public interface Parse<Result> {
        public Result parse(@Nullable String response);
    }

    public URL url;
    public String httpMethodName = "GET";
    public Parse<A> parseBlock;

    public Resource(@NonNull URL url, @NonNull Parse<A> parse) {
        this.url = url;
        this.parseBlock = parse;
    }

    public Resource(@NonNull URL url, @NonNull String httpMethodName, @NonNull Parse<A> parse) {
        this.url = url;
        this.httpMethodName = httpMethodName;
        this.parseBlock = parse;
    }
}