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

class ChunkyCanvasResamplingStepsResidues < Benchmarks 

  def initialize
    @canvas = MockCanvas.new
  end 

  def benchmark 
    @canvas.steps_residues(500_000, 1_000_000)
  end

  def verify_result(result)
    result[0][999999] == 499999 && result[1][999999] == 64
  end

  class MockCanvas
    
    if ENV['USE_CEXTS']
      include OilyPNG::Resampling
    else
      include ChunkyPNG::Canvas::Resampling
    end

    public :steps_residues
  end
end
