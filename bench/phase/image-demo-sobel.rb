require_relative '../image-demo/lib/sobel'

class ImageDemoSobel < Benchmarks
  def initialize
    @width = 240
    @height = 180
    @image = NoBorderImagePadded.new(@width, @height)
  end

  def benchmark
    sobel_magnitude_uint8(@image)
  end

  def verify_result(result)
    result.width == 240
  end 
end
