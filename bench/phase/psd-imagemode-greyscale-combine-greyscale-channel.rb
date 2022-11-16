# Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
require_relative '../psd.rb/psd.rb/lib/psd'
require_relative '../chunky_png/chunky_png/lib/chunky_png'
require_relative '../psd.rb/mock-logger'

WIDTH = 1000
HEIGHT = 1000

CHANNEL_DATA = [128] * WIDTH * HEIGHT * 2

class PsdImagemodeGreyscaleCombineGreyscaleChannel < Benchmarks
  def initialize
    require 'chunky_png/color'
    require 'psd/color'
    require 'psd/util'
    require 'psd/image_modes/greyscale'
    @image = MockImage.new
  end

  def benchmark
    @image.combine_greyscale_channel
    @image.reset
  end

  def verify_result(result)
    result.length == 0
  end

  class MockImage
    include PSD::ImageMode::Greyscale

    public :combine_greyscale_channel

    def initialize
      @num_pixels = WIDTH * HEIGHT
      @channel_length = @num_pixels
      @channel_data = CHANNEL_DATA
      @pixel_data = []
    end

    def channels
      2
    end

    def pixel_step
      2
    end

    def pixel_data
      @pixel_data
    end
    
    def reset
      @pixel_data.clear
    end
  end
end




