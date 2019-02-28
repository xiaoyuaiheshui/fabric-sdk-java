package yusq.hyperledger.example.demo;

import yusq.hyperledger.example.AppUser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.*;
import org.hyperledger.fabric_ca.sdk.exception.IdentityException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import yusq.hyperledger.example.HFJavaSDKBasicExample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Author: yusq
 * @Date: 2019/1/13 0013
 */
public class CaDemo {
    public static void main(String[] args) throws Exception {
        HFCAClient caClient = HFJavaSDKBasicExample.getHfCaClient("http://192.168.190.128:7054", null);
        AppUser admin = getAdmin(caClient);
        add(caClient,admin);
        query(caClient,admin);
//        query(caClient,admin);
//        AddUserIdentity(caClient,admin);
//        queryIdentity(caClient,admin);
//        AddUser(caClient,admin);
    }
    static void query(HFCAClient caClient,AppUser admin) throws Exception {
        HFCAAffiliation affiliation = caClient.newHFCAAffiliation("org1");
        affiliation.read(admin);
        System.out.println("=======");
        affiliation.getChildren().stream().map(f->f.getName()).forEach(System.out::println);
        System.out.println("=======");
        caClient.getHFCAAffiliations(admin).getChildren().stream().map(f->f.getName()).forEach(System.out::println);
    }
    static void add(HFCAClient caClient,AppUser admin) throws Exception {
        HFCAAffiliation affiliation = caClient.newHFCAAffiliation("org3");
        affiliation.create(admin);
    }

    static AppUser getAdmin(HFCAClient caClient) throws Exception {
        AppUser admin = tryDeserialize("admin");
        if (admin == null) {
            Enrollment adminEnrollment = caClient.enroll("admin", "adminpw");
            admin = new AppUser("admin", "org1", "Org1MSP", adminEnrollment);
            serialize(admin);
        }
        return admin;
    }
    static void queryByName() throws Exception {
//        AppUser admin = getAdmin();
        HFCAClient caClient = HFJavaSDKBasicExample.getHfCaClient("http://192.168.190.128:7054", null);
        HFCAIdentity identity = caClient.newHFCAIdentity("user1");
//        System.out.println(identity.read(admin));
    }

    static void AddUserIdentity(HFCAClient caClient,AppUser admin) throws InvalidArgumentException, IdentityException {
        HFCAIdentity identity = caClient.newHFCAIdentity("yusq");
        identity.setAffiliation("org1.department1");
        identity.setSecret("yusq123456.");
        identity.setType("client");
        System.out.println("==========");
        System.out.println(identity.create(admin));
    }
    static void AddUser(HFCAClient caClient,AppUser admin) throws Exception {
        AppUser user = null;
        RegistrationRequest rr = new RegistrationRequest("yushunquan","org1.department2");
        rr.setSecret("yu1234561");
        rr.setType("peer");
        Attribute revoker = new Attribute("hf.Revoker","true");
        rr.addAttribute(revoker);
        Attribute roles = new Attribute("hf.Registrar.Roles","user,client,peer");
        rr.addAttribute(roles);
        Attribute attrs = new Attribute("hf.Registrar.Attributes","hf.Revoker,hf.Registrar.Roles");
        rr.addAttribute(attrs);
        String passw =  caClient.register(rr,admin);
        Enrollment enrollment = caClient.enroll("yushunquan",passw);
        AppUser appUser = new AppUser("yushunquan","org1", "Org1MSP", enrollment);
        serialize(appUser);
    }
    static void queryIdentity(HFCAClient caClient,AppUser admin) throws InvalidArgumentException, IdentityException {
        HFCAIdentity identity = caClient.newHFCAIdentity("yusq");
        System.out.println("admin:"+identity.read(admin));
    }

    static AppUser tryDeserialize(String name) throws Exception {
        if (Files.exists(Paths.get(name + ".jso"))) {
            return deserialize(name);
        }
        return null;
    }
    static void serialize(AppUser appUser) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                Paths.get(appUser.getName() + ".jso")))) {
            oos.writeObject(appUser);
        }
    }
    static AppUser deserialize(String name) throws Exception {
        try (ObjectInputStream decoder = new ObjectInputStream(
                Files.newInputStream(Paths.get(name + ".jso")))) {
            return (AppUser) decoder.readObject();
        }
    }
}
