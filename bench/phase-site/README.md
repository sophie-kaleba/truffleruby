Adapted the Yjit benchmark to use truffleruby and rebench harness.
Our goal is to experiment whether having template-based phases to guide phase-based splitting would bring better performance.

Diff:
* The number of iterations (20 originally) is parametrized by the user (harness.rb sitebench 20 1)
* The benchmark inherits from the Benchmark class
* Some dependencies are unused by the current project and have been removed

# Jekyll-Perf Benchmark for YJIT

Based on https://github.com/agbell/jekyll-perf and https://earthly.dev/blog/jruby/ ("Why is JRuby Slow?")

For this benchmark we use agbell's provided Jekyll directory. We render it 20 times, as he does, but we use the yjit-bench harness to do so.

