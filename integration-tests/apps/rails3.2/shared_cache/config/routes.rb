CachingTest::Application.routes.draw do
  root :to => "main#index"
  match '/persisted' => 'main#persisted'
end
