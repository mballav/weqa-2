package com.weqa.framework;

/**
 * Created by Manish Ballav on 10/12/2017.
 */

public interface MyCall<T> {
    void cancel();

    void enqueue(MyCallback<T> callback);

    MyCall<T> clone();

    String getUrl();

    // Left as an exercise for the reader...
    // TODO MyResponse<T> execute() throws MyHttpException;
}
