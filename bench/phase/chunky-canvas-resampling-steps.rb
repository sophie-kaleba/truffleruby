# Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
if ENV['USE_CEXTS']
  require_relative '../chunky_png/chunky_png/lib/oily_png'
else
  require_relative '../chunky_png/chunky_png/lib/chunky_png'
end

class ChunkyCanvasResamplingSteps < Benchmarks 

  def initialize
    @canvas = MockCanvas.new
  end 

  def benchmark 
    @canvas.steps(1_000_000, 5_000_000)
  end

  def verify_result(result)
    result[4999999] == 999999
  end

  class MockCanvas
    
    if ENV['USE_CEXTS']
      include OilyPNG::Resampling
    else
      include ChunkyPNG::Canvas::Resampling
    end

    public :steps
  end
end
