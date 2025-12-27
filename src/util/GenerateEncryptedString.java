package util;

public class GenerateEncryptedString {
	public static void main(String[] args) {
		String email= "rajendra.singh068@gmail.com";
		System.out.println("Gmail ID:"+EncryptionUtil.encrypt(email));
	    String pw = "cddd xhrc wogd rouu"; // app password
	    System.out.println("APP Pwd Gmail Encrypted : "+EncryptionUtil.encrypt(pw));
	    
	    
	    String encrypted =  EncryptionUtil.encrypt("public");
	    System.out.println("DB_PWD:"+encrypted);
	    String decrypted =  EncryptionUtil.decrypt(encrypted);
	    System.out.println("DB_PWD Decrypted: "+decrypted);
	    
	   //DBUtil.testConnection();
	}

}
