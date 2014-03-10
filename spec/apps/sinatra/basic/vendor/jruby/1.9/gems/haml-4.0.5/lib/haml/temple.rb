require 'haml'
require 'temple'

module Haml

  module Temple

    module Expressions
      def on_plain
        [:static, "\n" + value[:text]]
      end

      def on_root
        [:multi]
      end

      def on_doctype
        [:html, :doctype, value[:version] || 5]
      end

      def on_tag
        exp = [:html, :tag, value[:name], [:html, :attrs]]
        if value[:value] && value[:value] != ""
          if value[:parse]
            exp.push << [:dynamic, value[:value]]
          else
            exp.push << [:static, value[:value]]
          end
        end

        if attribs = value[:attributes]
          attribs.each do |key, value|
            exp.last << [:html, :attr, key, [:static, value]]
          end
        end

        exp
      end
    end

    class Parser
      def initialize(*args)
        @options = Options.new
      end

      def call(haml)
        parser = ::Haml::Parser.new(haml, @options)
        tree = parser.parse.tap {|x| p x; puts '-' * 10}
        compile(tree).tap {|x| p x; puts '-' * 10}
      end

      private

      def compile(node)
        exp = node.to_temple
        return exp if node.children.empty?
        if node.children.length == 1
          exp.push compile(node.children[0])
        else
          exp.push [:multi, *node.children.map {|c| compile(c)}]
        end
        exp
      end
    end

    class Engine < ::Temple::Engine
      use ::Haml::Temple::Parser
      html :Pretty
      filter :ControlFlow
      generator :ArrayBuffer
    end
  end

  class Parser::ParseNode
    include ::Haml::Temple::Expressions

    def to_temple
      begin
        send "on_#{type}"
      end
    end
  end
end

Haml::Temple::Template = Temple::Templates::Tilt(Haml::Temple::Engine, :register_as => :haml)