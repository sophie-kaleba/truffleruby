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

class ChunkyOperationsReplace < Benchmarks 
  def initialize
    @a = MockCanvas.new
    @b = MockCanvas.new
  end

  def benchmark
    @a.replace!(@b, 0, 0)
  end

  def verify_result(result)
    result.width == 4000
  end

  class MockCanvas
    
    if ENV['USE_CEXTS']
      include OilyPNG::Operations
    else
      include ChunkyPNG::Canvas::Operations
    end

    public :compose!

    def initialize
      @pixels = Array.new(width * height, 0x12345678)
    end

    def width
      4000
    end

    def height
      4000
    end

    def pixels
      @pixels
    end

    def get_pixel(x, y)
      @pixels[y * width + x]
    end

    def set_pixel(x, y, color)
      @pixels[y * width + x] = color
    end
  end
end