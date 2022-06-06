# Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../psd.rb/psd.rb/lib/psd'
require_relative '../chunky_png/chunky_png/lib/chunky_png'

class PsdColorCmykToRgb < Benchmarks 
  def initialize
    require "psd/color"
    require "psd/util"
  end
  
  def benchmark
    PSD::Color::cmyk_to_rgb(196, 196, 196, 196)
  end

  def verify_result(result)
    result[:r] == 14 && result[:r] == result[:g] && result[:r] == result[:b]
  end
end
