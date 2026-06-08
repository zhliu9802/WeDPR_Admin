package com.webank.wedpr;

import com.webank.wedpr.components.initializer.WeDPRApplication;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.sync.ResourceSyncer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.webank"})
@Slf4j
public class WedprAdminApplication {

    public static void main(String[] args) throws Exception {
        long startT = System.currentTimeMillis();
        try {
            WeDPRApplication.main(args, "WeDPR-ADMIN");
            ResourceSyncer resourceSyncer =
                    WeDPRApplication.getApplicationContext().getBean(ResourceSyncer.class);
            log.info("start resourceSyncer");
            resourceSyncer.start();
            log.info("start resourceSyncer success");
            // Note: must start leaderElection after the resourceSyncer started
            LeaderElection leaderElection =
                    WeDPRApplication.getApplicationContext().getBean(LeaderElection.class);
            log.info("start leaderElection");
            leaderElection.start();
            log.info("start leaderElection success");
            System.out.println(
                    "WeDPR-ADMIN: start WedprAdminApplication success, timecost: "
                            + (System.currentTimeMillis() - startT)
                            + " ms.");
            System.out.println(
                    "Swagger URL(Dev Mode): http://localhost:6850/swagger-ui/index.html");
        } catch (Exception e) {
            log.error("WedprAdminApplication start failed", e);
        }
    }
}
