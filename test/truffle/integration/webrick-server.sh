#!/usr/bin/env bash

source test/truffle/common.sh.inc

jt check_test_port
jt ruby test/truffle/integration/webrick-server/webrick-server.rb & test_server
