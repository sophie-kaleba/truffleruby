#!/bin/bash

pwd
exec ruby ./tool/jt.rb --use jvm-ce ruby "$@"