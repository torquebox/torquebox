# This class purposely generates an error when eval'd by our injection
# parser to ensure the parser skips over such classes instead of
# halting deployment of the application.
class InjectionParseError < <%= class_name %>
end
