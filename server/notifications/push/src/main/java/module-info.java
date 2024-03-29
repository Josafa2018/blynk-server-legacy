/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 29.09.17.
 */
open module cc.blynk.server.notifications.push {
    requires org.apache.logging.log4j;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.netty.codec.http;
    requires transitive async.http.client;
    requires transitive cc.blynk.utils;
    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.transport;
    requires org.slf4j;
    requires org.reactivestreams;

    exports cc.blynk.server.notifications.push;
    exports cc.blynk.server.notifications.push.android;
    exports cc.blynk.server.notifications.push.enums;
    exports cc.blynk.server.notifications.push.ios;
}
