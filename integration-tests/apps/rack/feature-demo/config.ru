
# Figure out where bundler stashed our feature-demo source
git_path = Bundler.environment.dependencies.find do |dependency|
  dependency.name == 'torquebox-feature-demo'
end.source.path

# Hand everything over to the feature-demo's config.ru
$LOAD_PATH << git_path
eval File.read("#{git_path}/config.ru")
