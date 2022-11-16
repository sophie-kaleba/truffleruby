$LOAD_PATH.unshift "#{__dir__}/../rubykon"
$LOAD_PATH.unshift "#{__dir__}/../rubykon/rubykon/lib"
require 'rubykon'

class RubykonBench < Benchmarks

  def initialize
    @game_state = Rubykon::GameState.new Rubykon::Game.new(19)
    @mcts = MCTS::MCTS.new
  end

  def benchmark
    @mcts.start @game_state, 1_000
  end

  def verify_result(result)
    true
  end
end
