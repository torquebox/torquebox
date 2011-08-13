
module org.projectodd.stilts.stomplet::StompletConfig

  def [](name)
    self.getProperty( name.to_s )
  end

end
