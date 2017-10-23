package com.weqa.framework;

/**
 * Created by Manish Ballav on 10/12/2017.
 */


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;



/**
 * A sample showing a custom {@link CallAdapter} which adapts the built-in {@link Call} to a custom
 * version whose callback has more granular methods.
 */
public final class ErrorHandlingAdapter {

    public static class ErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
        @Override
        public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
                                     Retrofit retrofit) {
            if (getRawType(returnType) != MyCall.class) {
                return null;
            }
            if (!(returnType instanceof ParameterizedType)) {
                throw new IllegalStateException(
                        "MyCall must have generic type (e.g., MyCall<ResponseBody>)");
            }
            Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
            Executor callbackExecutor = retrofit.callbackExecutor();
            return new ErrorHandlingCallAdapter<>(responseType, callbackExecutor);
        }

        private static final class ErrorHandlingCallAdapter<R> implements CallAdapter<R, MyCall<R>> {
            private final Type responseType;
            private final Executor callbackExecutor;

            ErrorHandlingCallAdapter(Type responseType, Executor callbackExecutor) {
                this.responseType = responseType;
                this.callbackExecutor = callbackExecutor;
            }

            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public MyCall<R> adapt(Call<R> call) {
                return new MyCallAdapter<>(call, callbackExecutor);
            }
        }
    }

    /**
     * Adapts a {@link Call} to {@link MyCall}.
     */
    static class MyCallAdapter<T> implements MyCall<T> {
        private final Call<T> call;
        private final Executor callbackExecutor;

        MyCallAdapter(Call<T> call, Executor callbackExecutor) {
            this.call = call;
            this.callbackExecutor = callbackExecutor;
        }

        @Override
        public void cancel() {
            call.cancel();
        }

        @Override
        public String getUrl() {
            return call.request().url().url().toString();
        }

        @Override
        public void enqueue(final MyCallback<T> callback) {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    // TODO if 'callbackExecutor' is not null, the 'callback' methods should be executed
                    // on that executor by submitting a Runnable. This is left as an exercise for the reader.

                    int code = response.code();
                    if (code >= 200 && code < 300) {
                        callback.success(response);
                    } else if (code == 401) {
                        callback.unauthenticated(response);
                    } else if (code >= 400 && code < 500) {
                        callback.clientError(response);
                    } else if (code >= 500 && code < 600) {
                        callback.serverError(response);
                    } else {
                        callback.unexpectedError(new RuntimeException("Unexpected response " + response));
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    // TODO if 'callbackExecutor' is not null, the 'callback' methods should be executed
                    // on that executor by submitting a Runnable. This is left as an exercise for the reader.

                    if (t instanceof IOException) {
                        callback.networkError((IOException) t);
                    } else {
                        callback.unexpectedError(t);
                    }
                }
            });
        }

        @Override
        public MyCall<T> clone() {
            return new MyCallAdapter<>(call.clone(), callbackExecutor);
        }
    }

}
