package io.mybatis.provider.util;

/**
 * 断言异常
 *
 * @author liuzh
 */
public class AssertException extends RuntimeException {
  public AssertException() {
  }

  public AssertException(String message) {
    super(message);
  }

  public AssertException(String message, Throwable cause) {
    super(message, cause);
  }

  public AssertException(Throwable cause) {
    super(cause);
  }

  public AssertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
