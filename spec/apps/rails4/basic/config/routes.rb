Basic::Application.routes.draw do
  root :to => 'root#index'
  get 'reloader/:version' => 'reloader#index'
  get ':controller(/:action(/:id))(.:format)'
end
