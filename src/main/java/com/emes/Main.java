package com.emes;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Main {

  public static void main(String[] args)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException,
      InvalidKeyException {

    new MainRunner().run(args);
  }
}