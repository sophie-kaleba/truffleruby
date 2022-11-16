# Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
require_relative '../psd.rb/psd.rb/lib/psd'
require_relative '../psd.rb/mock-logger'

WIDTH = 1000
HEIGHT = 1000

CHANNEL_DATA = [128] * WIDTH * HEIGHT * 5

class PsdImagemodeCmykCombineCmykChannel < Benchmarks
  def initialize
    require 'chunky_png/color'
    require 'psd/color'
    require 'psd/util'
    require 'psd/image_modes/cmyk'

    @image = MockImage.new
  end

  def benchmark
    @image.combine_cmyk_channel
    @image.reset
  end

  def verify_result(result)
    result.length == 0
  end

  class MockImage
    include PSD::ImageMode::CMYK

    public :combine_cmyk_channel

    def initialize
      @num_pixels = WIDTH * HEIGHT
      @channels_info = [{id: 0}, {id: 1}, {id: 2}, {id: 3}, {id: -1}]
      @channel_length = @num_pixels
      @channel_data = CHANNEL_DATA
      @pixel_data = []
    end

    def pixel_step
      1
    end

    def pixel_data
      @pixel_data
    end
    
    def reset
      @pixel_data.clear
    end
  end
end
