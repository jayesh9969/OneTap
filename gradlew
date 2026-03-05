#!/bin/sh
# Gradle wrapper - uses system gradle with custom JAVA_HOME
export JAVA_HOME=/root/jdk-17.0.9+9
cd /root/.openclaw/workspace/projects/OneTap
/root/gradle-8.5/bin/gradle "$@"
