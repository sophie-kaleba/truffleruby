# Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../psd.rb/psd.rb/lib/psd'

class PsdUtilClamp < Benchmarks
  def initialize
    require 'psd/util'
  end

  def benchmark
    PSD::Util::clamp(14, 10, 20)
  end

  def verify_result(result)
    result == 14
  end
end