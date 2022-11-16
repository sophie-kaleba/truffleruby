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

class ChunkyCanvasResamplingNearestNeighbor < Benchmarks 

  def initialize
    @canvas = MockCanvas.new
  end 

  def benchmark 
    @canvas.resample_nearest_neighbor!(500, 500)
  end

  def verify_result(result)
    result[0] == 305419896
  end
    
  class MockCanvas
    
    if ENV['USE_CEXTS']
      include OilyPNG::Resampling
    else
      include ChunkyPNG::Canvas::Resampling
    end

    public :resample_nearest_neighbor!

    def initialize
      @pixels = Array.new(width * height, 0)

      @pixels.size.times do |n|
        @pixels[n] = 0x12345678
      end
    end

    def width
      100
    end

    def height
      100
    end

    def pixels
      @pixels
    end

    def get_pixel(x, y)
      @pixels[y * width + x]
    end

    def replace_canvas!(new_width, new_height, pixels)
      @width = new_width
      @height = new_height
      @pixels = pixels
    end
  end
end
