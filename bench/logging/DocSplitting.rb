class DocSplitting

  def add(arg1, arg2)
    arg1 + arg2
    #arg1.capitalize()
  end

  def double(arg1)
    add(arg1, arg1)
  end

  def callsDouble 
    double(1)
    #double(:toto)
    double("foo")
  end

  def run 
    i = 0
    while i < 1000 do
      callsDouble()
      i += 1
    end
  end

end