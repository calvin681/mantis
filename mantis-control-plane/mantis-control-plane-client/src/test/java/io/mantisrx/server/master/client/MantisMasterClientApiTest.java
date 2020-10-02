/*
 * Copyright 2019 Netflix, Inc.
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

package io.mantisrx.server.master.client;

//import io.mantisrx.master.api.proto.CreateJobClusterRequest;
//import io.mantisrx.master.api.proto.SubmitJobRequest;
//import io.mantisrx.master.core.proto.JobDefinition;
//import io.mantisrx.master.core.proto.MachineDefinition;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.mantisrx.server.core.master.MasterDescription;
import io.mantisrx.server.core.master.MasterMonitor;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import mantis.io.reactivex.netty.RxNetty;
import mantis.io.reactivex.netty.pipeline.PipelineConfigurators;
import mantis.io.reactivex.netty.protocol.http.server.HttpServer;
import mantis.io.reactivex.netty.protocol.http.server.HttpServerRequest;
import mantis.io.reactivex.netty.protocol.http.server.HttpServerResponse;
import mantis.io.reactivex.netty.protocol.http.server.RequestHandler;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;


public class MantisMasterClientApiTest {

    private static final Logger logger = LoggerFactory.getLogger(MantisMasterClientApiTest.class);
    private static AtomicInteger port = new AtomicInteger(8950);
    private static List<HttpServer<String, String>> startedServers = new ArrayList<>();

    @AfterClass
    public static void cleanup() throws InterruptedException {
        for (HttpServer<String, String> startedServer : startedServers) {
            logger.info("shutting down server on port {}", startedServer.getServerPort());
            startedServer.shutdown();
        }
    }

    public HttpServer<String, String> createHttpServer(int port) {
        final HttpServer<String, String> server = RxNetty.newHttpServerBuilder(
                port,
                new RequestHandler<String, String>() {
                    @Override
                    public Observable<Void> handle(HttpServerRequest<String> req, HttpServerResponse<String> resp) {
                        resp.writeAndFlush("200 OK");
                        return Observable.empty();
                    }
                })
                .pipelineConfigurator(PipelineConfigurators.httpServerConfigurator())
                .channelOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                .build();
        return server;
    }

    //    @Test
    //    @Ignore
    //    public void testNamedJobCreate() throws InterruptedException {
    //
    //        MasterMonitor mockMasterMonitor = mock(MasterMonitor.class);
    //        final BehaviorSubject<MasterDescription> mdSubject = BehaviorSubject.create();
    //        when(mockMasterMonitor.getMasterObservable()).thenReturn(mdSubject);
    //
    //        MantisMasterClientApi mantisMasterClientApi = new MantisMasterClientApi(mockMasterMonitor);
    //
    //        final int serverPort = 8182;
    //        final String jobName = "TestCreateJobCluster";
    //        mdSubject.onNext(new MasterDescription("localhost", "127.0.0.1", serverPort, 7090, 7091, "status", 8900, System.currentTimeMillis()));
    //
    //        JobDefinition jobDefinition = JobDefinition.newBuilder()
    //            .setName(jobName)
    //            .setVersion("0.0.1")
    //            .setUrl("http://www.example.com")
    //            .setJobSla(io.mantisrx.master.core.proto.JobSla.newBuilder()
    //                .setUserProvidedType("")
    //                .setDurationType(io.mantisrx.master.core.proto.JobSla.MantisJobDurationType.Transient)
    //                .setSlaType(io.mantisrx.master.core.proto.JobSla.StreamSLAType.Lossy)
    //                .setMinRuntimeSecs(0)
    //                .setRuntimeLimitSecs(0))
    //            .setSchedulingInfo(io.mantisrx.master.core.proto.SchedulingInfo.newBuilder()
    //                .putStages(1, io.mantisrx.master.core.proto.SchedulingInfo.StageSchedulingInfo.newBuilder()
    //                    .setNumberOfInstances(1)
    //                    .setMachineDefinition(MachineDefinition.newBuilder()
    //                        .setCpuCores(2)
    //                        .setDiskMB(1024)
    //                        .setMemoryMB(2048)
    //                        .setNetworkMbps(64)
    //                        .setNumPorts(1)
    //                        .build())
    //                    .build())
    //                .build())
    //            .build();
    //        io.mantisrx.master.core.proto.JobOwner owner = io.mantisrx.master.core.proto.JobOwner.newBuilder()
    //            .setName("Test")
    //            .setContactEmail("test@netflix.com")
    //            .setDescription("")
    //            .setRepo("http://www.example.com")
    //            .build();
    //        CreateJobClusterRequest req = CreateJobClusterRequest.newBuilder()
    //            .setJobDefinition(jobDefinition)
    //            .setOwner(owner)
    //            .build();
    //
    //        Observable<Void> testCluster = mantisMasterClientApi.createNamedJob(req);
    //        final CountDownLatch latch = new CountDownLatch(1);
    //
    //        testCluster.subscribe((x) -> {
    //            latch.countDown();
    //            System.out.println("job cluster create response complete");
    //        });
    //
    //        latch.await();
    //
    //        Observable<JobSubmitResponse> jobSubmitResponseObs = mantisMasterClientApi.submitJob(jobDefinition);
    //        final CountDownLatch latch2 = new CountDownLatch(1);
    //
    //        jobSubmitResponseObs.subscribe((x) -> {
    //            latch2.countDown();
    //            System.out.println("job submit complete");
    //        });
    //
    //        latch2.await();
    //    }

    @Test
    public void testScaleStageRequestRetries() throws InterruptedException {

        MasterMonitor mockMasterMonitor = mock(MasterMonitor.class);
        final BehaviorSubject<MasterDescription> mdSubject = BehaviorSubject.create();
        when(mockMasterMonitor.getMasterObservable()).thenReturn(mdSubject);

        MantisMasterClientApi mantisMasterClientApi = new MantisMasterClientApi(mockMasterMonitor);


        final int serverPort = port.incrementAndGet();
        final String jobId = "test-job-id";
        final int stageNum = 1;
        final int numWorkers = 2;
        final String reason = "test reason";
        mdSubject.onNext(new MasterDescription("localhost", "127.0.0.1", serverPort, 7090, 7091, "status", 8900, System.currentTimeMillis()));

        final CountDownLatch retryLatch = new CountDownLatch(2);

        final Func1<Observable<? extends Throwable>, Observable<?>> retryLogic = new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> attempts) {
                return attempts
                        .zipWith(Observable.range(1, 5), new Func2<Throwable, Integer, Integer>() {
                            @Override
                            public Integer call(Throwable t1, Integer integer) {
                                return integer;
                            }
                        })
                        .flatMap(new Func1<Integer, Observable<?>>() {
                            @Override
                            public Observable<?> call(Integer retryCount) {
                                logger.info(retryCount + " retrying conx after sleeping for 250ms");
                                if (retryCount == 2) {
                                    Schedulers.newThread().createWorker().schedule(new Action0() {
                                        @Override
                                        public void call() {
                                            final HttpServer<String, String> httpServer = createHttpServer(serverPort);
                                            startedServers.add(httpServer);
                                            httpServer.start();
                                        }
                                    });
                                }
                                retryLatch.countDown();
                                return Observable.timer(250, TimeUnit.MILLISECONDS);
                            }
                        });
            }
        };

        final Observable<Boolean> resultObs = mantisMasterClientApi.scaleJobStage(jobId, stageNum, numWorkers, reason)
                .retryWhen(retryLogic);

        final CountDownLatch completedLatch = new CountDownLatch(1);

        resultObs
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        fail("got unexpected error" + throwable.getMessage());
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        completedLatch.countDown();
                    }
                }).subscribe();

        assertTrue(retryLatch.await(5, TimeUnit.SECONDS));
        assertTrue(completedLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testScaleStageRequestRetriesNewMaster() throws InterruptedException {

        MasterMonitor mockMasterMonitor = mock(MasterMonitor.class);
        final BehaviorSubject<MasterDescription> mdSubject = BehaviorSubject.create();
        when(mockMasterMonitor.getMasterObservable()).thenReturn(mdSubject);

        MantisMasterClientApi mantisMasterClientApi = new MantisMasterClientApi(mockMasterMonitor);


        final int oldMasterPort = port.incrementAndGet();
        final int newMasterPort = port.incrementAndGet();

        final String jobId = "test-job-id";
        final int stageNum = 1;
        final int numWorkers = 2;
        final String reason = "test reason";
        mdSubject.onNext(new MasterDescription("localhost", "127.0.0.1", oldMasterPort, 7090, 7091, "status", 8900, System.currentTimeMillis()));

        final CountDownLatch retryLatch = new CountDownLatch(3);

        final Func1<Observable<? extends Throwable>, Observable<?>> retryLogic = new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> attempts) {
                return attempts
                        .zipWith(Observable.range(1, 5), new Func2<Throwable, Integer, Integer>() {
                            @Override
                            public Integer call(Throwable t1, Integer integer) {
                                return integer;
                            }
                        })
                        .flatMap(new Func1<Integer, Observable<?>>() {
                            @Override
                            public Observable<?> call(Integer retryCount) {
                                logger.info(retryCount + " retrying conx after sleeping for 250ms");
                                if (retryCount == 2) {
                                    Schedulers.newThread().createWorker().schedule(new Action0() {
                                        @Override
                                        public void call() {
                                            final HttpServer<String, String> httpServer = createHttpServer(newMasterPort);
                                            startedServers.add(httpServer);
                                            httpServer.start();
                                        }
                                    });
                                }
                                if (retryCount == 3) {
                                    mdSubject.onNext(new MasterDescription("localhost", "127.0.0.1", newMasterPort, 7090, 7091, "status", 8900, System.currentTimeMillis()));
                                }
                                retryLatch.countDown();
                                return Observable.timer(250, TimeUnit.MILLISECONDS);
                            }
                        });
            }
        };

        final Observable<Boolean> resultObs = mantisMasterClientApi.scaleJobStage(jobId, stageNum, numWorkers, reason)
                .retryWhen(retryLogic);

        final CountDownLatch completedLatch = new CountDownLatch(1);

        resultObs
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        fail("got unexpected error" + throwable.getMessage());
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        completedLatch.countDown();
                    }
                }).subscribe();

        assertTrue(retryLatch.await(5, TimeUnit.SECONDS));
        assertTrue(completedLatch.await(5, TimeUnit.SECONDS));
    }
}
