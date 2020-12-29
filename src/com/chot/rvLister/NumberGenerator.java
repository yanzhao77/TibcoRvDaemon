package com.chot.rvLister;

import com.tibco.tibrv.*;

import java.io.UnsupportedEncodingException;

public class NumberGenerator {
    private String service = "7500";
    private String network = ";225.1.1.1";
    private String daemons = "10.56.14.176:7500";
    private String subject = "DEMO.Demosvr";


    public void run() {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            TibrvMsg.setStringEncoding("Big5");
            TibrvTransport transport = new TibrvRvdTransport(service, network, daemons);

            for (int i = 1; i <= 1000; i++) {
                TibrvMsg msg = new TibrvMsg();
                msg.setSendSubject(subject);
                msg.update("number", i);
                System.out.println("number" + i);
                transport.send(msg);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            Tibrv.close();
        } catch (TibrvException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NumberGenerator gen = new NumberGenerator();
        gen.run();
        System.out.println("stop");
    }
}