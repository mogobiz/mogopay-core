package mogopay.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Sha512 {
	public static String hmacDigest(String msg, String keyString) {
		String digest = null;
		try {
            final byte[] bytesKey = DatatypeConverter.parseHexBinary(keyString);
            final SecretKeySpec key = new SecretKeySpec(bytesKey, "HmacSHA512");
            final Mac mac = Mac.getInstance("HmacSHA512");
			mac.init(key);

			final byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

			StringBuffer hash = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();
		} catch (UnsupportedEncodingException e) {
		} catch (InvalidKeyException e) {
		} catch (NoSuchAlgorithmException e) {
		}
		return digest.toUpperCase();
	}
	public static void main(String[] args) {
		System.out.println(hmacDigest("PBX_SITE=1999888", "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"));
	}
}
