Basic::Application.routes.draw do
  root :to => 'root#index'
  match 'reloader/:version' => 'reloader#index'
  match ':controller(/:action(/:id))(.:format)'
end
