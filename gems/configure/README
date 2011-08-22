= Extending the configuration DSL

To add something to the DSL, you just need to modify lib/configuration/global.rb
in two places. As an example, we'll add two new keywords to our dsl: stomp & stomplet:

* stomp is used to set overall options for the stomp functionality, such as host and port. 
  We want to be able to call it multiple times, with the settings being merged into one hash,
  no matter how many times it is called

* stomplet is used to define a stomplet that will be instantiated. Each call should create 
  a discrete config for that stomplet.

== Adding the keywords

The ENTRY_MAP in GlobalConfiguration (global.rb) defines all of the keywords for the dsl. It is
a hash of keyword-as-a-symbol to the Entry class that implements the keyword. lib/configuration.rb 
defines the following Entry classes:
 
* ThingEntry: foo :bar
* OptionsEntry: foo :bar => :baz
* ThingWithOptionsEntry: foo :bar, :ham => :biscuit

The Entry classes can be customized with the with_settings() class method to add validation and more.

First, let's add the stomp keyword. Let's assume it is just a simple hash of options, and those options
can only be :host and :port. We want to be able to specify it like so:

    stomp :host => 'ham.biscu.it'

and:
    
    stomp {
      port 59009
    }


To support this, we add the following to the ENTRY_MAP:

    :stomp => OptionsEntry.with_settings( :validate => { :optional => [:host, :port] } )

Now let's add stomplet. Let's assume you must always specify the class that implements the
stomplet, along with the route. You can optionally specify a name and a boolean fungible option
for the stomplet as well. We also want each stomplet call to create a discrete stomplet. 
We want to be able to specify it as:

    stomplet MyStomplet, :route => '/a/b/:c', :fungible => true

and 

    stomplet MyStomplet do 
      route '/a/b/:c'
      name 'foobar'
      fungible false
    end

To support this, we add the following to the ENTRY_MAP:

   :stomplet => ThingWithOptionsEntry.with_settings( :discrete => true,
                                                     :validate => {
                                                       :required => [ :route ],
                                                       :optional => [ :name, { :fungible => [ true, false ] } ] } )
                                                                 
The :discrete setting signals this keyword to accumulate entries in
an array instead of merging into a hash based of off the 'thing' ('MyStomplet' in 
this case). This allows for multiple stomplets using the same class. 

== Converting to the expected yaml format

The DSL has its own simple hash and array based representation of the configuration. Since
the output of the DSL gets fed into the front of the *YamlParsingProcessor chain, we have
to convert the configuration into the same format that the yaml parser spits out. That's
all controlled by the to_metadata_hash method in global.rb.

Assuming the stomp and stomplet yaml configuration looks like:

    stomp:
      host: ham.biscu.it
      stomplets:
        ham.stomplet:
          class: MyStomplet
          route: '/a/b/:c'
          fungible: true

This will result in a hash format like:

    { 'stomp' => 
      { 'host' => 'ham.biscu.it',
        'stomplets' => {
          'ham.stomplet' => {
            'class' => 'MyStomplet',
            'route' => '/a/b/:c',
            'fungible' => true } } } }
    
Since that is the format the StompYamlParsingProcessor will expect, we'll need to convert
from the DSL's internal format to the above. Our DSL examples above will result in the 
following internal representation:

    { 'stomp' => { 'host' => 'ham.biscu.it' },
      'stomplet' => [ 'MyStomplet', { 'route' => '/a/b/:c', 'name' => 'foobar', 'fungible' => false } ] }

To add our conversion, simply add when blocks to the case statement in to_metadata_hash:     

    # for stomplet
    when 'stomplet' 
      entry_data.each do |klass, data|
        name = data.delete( :name ) || unique_name( klass.to_s, metadata['stomp']['stomplets'].keys )
        stomplet = metadata['stomp']['stomplets'][name] = {}
        stomplet['class'] = klass.to_s
        stomplet.merge!( data )
       end

   # for stomp, we just fall through to the default case

== Testing

You should add tests to global_spec.rb and the torquebox_rb_spec.rb integ. I leave those
as an exercise for the reader.




