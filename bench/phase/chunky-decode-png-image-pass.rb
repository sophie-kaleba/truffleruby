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

class ChunkyDecodePngImagePass < Benchmarks 
  def initialize
    @WIDTH = 400
    @HEIGHT = 400
    @COLOR_MODE = ChunkyPNG::COLOR_TRUECOLOR_ALPHA
    @DEPTH = 8
    @PIXEL = 0x12345678

    pixel = [@PIXEL].pack("N")
    scan_line = [ChunkyPNG::FILTER_NONE].pack("c") + (pixel * @WIDTH)
    @stream = scan_line * @HEIGHT
  end

  def benchmark
    MockCanvas::decode_png_image_pass(@stream, @WIDTH, @HEIGHT, @COLOR_MODE, @DEPTH, 0)
  end

  def verify_result(result)
    result.width == @WIDTH
  end

  class MockCanvas

    if ENV['USE_CEXTS']
      extend OilyPNG::PNGDecoding
    else
      extend ChunkyPNG::Canvas::PNGDecoding
    end

    class << self
      public :decode_png_image_pass
    end

    def initialize(width, height, pixels)
      @width = width
      @height = height
      @pixels = pixels
    end

    def width
      @width
    end

    def height
      @height
    end

    def pixels
      @pixels
    end
  end
end


