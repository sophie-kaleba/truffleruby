# -*- encoding: utf-8 -*-
# stub: geom2d 0.3.1 ruby lib

Gem::Specification.new do |s|
  s.name = "geom2d".freeze
  s.version = "0.3.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Thomas Leitner".freeze]
  s.date = "2019-11-27"
  s.email = "t_leitner@gmx.at".freeze
  s.homepage = "https://geom2d.gettalong.org".freeze
  s.licenses = ["MIT".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.4".freeze)
  s.rubygems_version = "3.3.7".freeze
  s.summary = "Objects and Algorithms for 2D Geometry".freeze

  s.installed_by_version = "3.3.7" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_development_dependency(%q<rubocop>.freeze, ["~> 0.58", ">= 0.58.2"])
  else
    s.add_dependency(%q<rubocop>.freeze, ["~> 0.58", ">= 0.58.2"])
  end
end
