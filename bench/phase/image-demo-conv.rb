require_relative '../image-demo/lib/noborder'

class ImageDemoConv < Benchmarks
  def initialize
    @width = 240
    @height = 180
    @image = NoBorderImagePadded.new(@width, @height)
    @conv = NoBorderImagePadded.new(3, 3)
  end

  def benchmark
    conv3x3(@image, @conv)
  end

  def verify_result(result)
    result.width == 240
  end 
end
