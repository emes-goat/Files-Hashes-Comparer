package com.emes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.junit.jupiter.api.Test;

class AESTest {

  @Test
  public void aesTest()
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {

    var aes = new AES();
    var secretContent = "This is my very secret text";
    var password = "Ultra secret password";

    var encrypted = aes.encrypt(password.toCharArray(), secretContent.getBytes());

    var decrypted = aes.decrypt(password.toCharArray(), encrypted);

    assertEquals(secretContent, new String(decrypted));
  }
}