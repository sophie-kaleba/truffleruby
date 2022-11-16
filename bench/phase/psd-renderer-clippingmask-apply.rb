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

SIZE = 2_000
PIXEL = 0x11223344

class PsdRendererClippingmaskApply < Benchmarks
  def initialize
    require 'psd/renderer/clipping_mask'
    require 'chunky_png/color'
    require 'psd/color'
    require 'psd/util'

    @mask = MockClippingMask.new
  end

  def benchmark
    @mask.apply!
  end

  def verify_result(result)
    result == 2000
  end

  class MockNode
    def clipped?
      true
    end

    def name
      "name"
    end
  end

  class MockCanvas
    def initialize
      @pixels = [PIXEL] * SIZE * SIZE
    end

    def top
      0
    end

    def left
      0
    end

    def width
      SIZE
    end

    def height
      SIZE
    end

    def pixels
      @pixels
    end

    def [](x, y)
      @pixels[y * SIZE + x]
    end

    def []=(x, y, pixel)
      @pixels[y * SIZE + x] = pixel
    end

    def canvas
      self
    end
  end

  class MockMask
    def initialize
      @node = MockNode.new
      @canvas = MockCanvas.new
    end

    def top
      0
    end

    def left
      0
    end

    def width
      SIZE
    end

    def height
      SIZE
    end

    attr_reader :node
    attr_reader :canvas
  end

  class MockClippingMask < PSD::Renderer::ClippingMask
    public :apply!

    def initialize
      @node = MockNode.new
      @canvas = MockCanvas.new
      @mask = MockMask.new
    end

    attr_reader :node
    attr_reader :canvas
    attr_reader :mask
  end
end