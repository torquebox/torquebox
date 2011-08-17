

module org.projectodd.stilts.stomp.spi::StompSession

  def [](name)
    self.getAttribute( name.to_s )
  end

  def []=(name,value)
    self.setAttribute(name.to_s, value)
  end

  def delete(name)
    self.removeAttribute( name.to_s )
  end

end
