/**
 * Copyright 2016 Yahoo Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yahoo.pulsar.common.configuration;

import static com.yahoo.pulsar.common.configuration.PulsarConfigurationLoader.isComplete;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.testng.annotations.Test;

import com.yahoo.pulsar.broker.ServiceConfiguration;

public class PulsarConfigurationLoaderTest {

    @Test
    public void testPulsarConfiguraitonLoadingStream() throws Exception {
        File testConfigFile = new File("tmp." + System.currentTimeMillis() + ".properties");
        if (testConfigFile.exists()) {
            testConfigFile.delete();
        }
        final String zkServer = "z1.example.com,z2.example.com,z3.example.com";
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(testConfigFile)));
        printWriter.println("zookeeperServers=" + zkServer);
        printWriter.println("globalZookeeperServers=gz1.example.com,gz2.example.com,gz3.example.com/foo");
        printWriter.println("brokerDeleteInactiveTopicsEnabled=true");
        printWriter.println("statusFilePath=/tmp/status.html");
        printWriter.println("managedLedgerDefaultEnsembleSize=1");
        printWriter.println("backlogQuotaDefaultLimitGB=18");
        printWriter.println("clusterName=usc");
        printWriter.println("brokerClientAuthenticationPlugin=test.xyz.client.auth.plugin");
        printWriter.println("brokerClientAuthenticationParameters=role:my-role");
        printWriter.println("superUserRoles=appid1,appid2");
        printWriter.println("brokerServicePort=7777");
        printWriter.println("managedLedgerDefaultMarkDeleteRateLimit=5.0");
        printWriter.close();
        testConfigFile.deleteOnExit();
        InputStream stream = new FileInputStream(testConfigFile);
        final ServiceConfiguration serviceConfig = PulsarConfigurationLoader.create(stream, ServiceConfiguration.class);
        assertNotNull(serviceConfig);
        assertEquals(serviceConfig.getZookeeperServers(), zkServer);
        assertEquals(serviceConfig.isBrokerDeleteInactiveTopicsEnabled(), true);
        assertEquals(serviceConfig.getBacklogQuotaDefaultLimitGB(), 18);
        assertEquals(serviceConfig.getClusterName(), "usc");
        assertEquals(serviceConfig.getBrokerClientAuthenticationParameters(), "role:my-role");
        assertEquals(serviceConfig.getBrokerServicePort(), 7777);
    }

    @Test
    public void testPulsarConfiguraitonLoadingProp() throws Exception {
        final String zk = "localhost:2184";
        final Properties prop = new Properties();
        prop.setProperty("zookeeperServers", zk);
        final ServiceConfiguration serviceConfig = PulsarConfigurationLoader.create(prop, ServiceConfiguration.class);
        assertNotNull(serviceConfig);
        assertEquals(serviceConfig.getZookeeperServers(), zk);
    }

    @Test
    public void testPulsarConfiguraitonComplete() throws Exception {
        final String zk = "localhost:2184";
        final Properties prop = new Properties();
        prop.setProperty("zookeeperServers", zk);
        final ServiceConfiguration serviceConfig = PulsarConfigurationLoader.create(prop, ServiceConfiguration.class);
        try {
            isComplete(serviceConfig);
            fail("it should fail as config is not complete");
        } catch (IllegalArgumentException e) {
            // Ok
        }
    }

    @Test
    public void testComplete() throws Exception {
        TestCompleteObject complete = this.new TestCompleteObject();
        assertTrue(isComplete(complete));
    }

    @Test
    public void testInComplete() throws IllegalAccessException {

        try {
            isComplete(this.new TestInCompleteObjectRequired());
            fail("Should fail w/ illegal argument exception");
        } catch (IllegalArgumentException iae) {
            // OK, expected
        }

        try {
            isComplete(this.new TestInCompleteObjectMin());
            fail("Should fail w/ illegal argument exception");
        } catch (IllegalArgumentException iae) {
            // OK, expected
        }

        try {
            isComplete(this.new TestInCompleteObjectMax());
            fail("Should fail w/ illegal argument exception");
        } catch (IllegalArgumentException iae) {
            // OK, expected
        }

        try {
            isComplete(this.new TestInCompleteObjectMix());
            fail("Should fail w/ illegal argument exception");
        } catch (IllegalArgumentException iae) {
            // OK, expected
        }
    }

    class TestCompleteObject {
        @FieldContext(required = true)
        String required = "I am not null";
        @FieldContext(required = false)
        String optional;
        @FieldContext
        String optional2;
        @FieldContext(minValue = 1)
        int minValue = 2;
        @FieldContext(minValue = 1, maxValue = 3)
        int minMaxValue = 2;

    }

    class TestInCompleteObjectRequired {
        @FieldContext(required = true)
        String inValidRequired;
    }

    class TestInCompleteObjectMin {
        @FieldContext(minValue = 1, maxValue = 3)
        long inValidMin = 0;
    }

    class TestInCompleteObjectMax {
        @FieldContext(minValue = 1, maxValue = 3)
        long inValidMax = 4;
    }

    class TestInCompleteObjectMix {
        @FieldContext(required = true)
        String inValidRequired;
        @FieldContext(minValue = 1, maxValue = 3)
        long inValidMin = 0;
        @FieldContext(minValue = 1, maxValue = 3)
        long inValidMax = 4;
    }
}
