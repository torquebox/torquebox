class ThingsController < ApplicationController
  # GET /things
  # GET /things.json
  def index
    @things = Thing.all

    respond_to do |format|
      format.html # index.html.erb
      format.json { render :json => @things }
    end
  end

  # GET /things/1
  # GET /things/1.json
  def show
    @thing = Thing.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.json { render :json => @thing }
    end
  end

  # GET /things/new
  # GET /things/new.json
  def new
    @thing = Thing.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render :json => @thing }
    end
  end

  # GET /things/1/edit
  def edit
    @thing = Thing.find(params[:id])
  end

  # POST /things
  # POST /things.json
  def create
    @thing = Thing.new(params[:thing])

    respond_to do |format|
      if @thing.save
        format.html { redirect_to @thing, :notice => 'Thing was successfully created.' }
        format.json { render :json => @thing, :status => :created, :location => @thing }
      else
        format.html { render :action => "new" }
        format.json { render :json => @thing.errors, :status => :unprocessable_entity }
      end
    end
  end

  # PUT /things/1
  # PUT /things/1.json
  def update
    @thing = Thing.find(params[:id])

    respond_to do |format|
      if @thing.update_attributes(params[:thing])
        format.html { redirect_to @thing, :notice => 'Thing was successfully updated.' }
        format.json { head :ok }
      else
        format.html { render :action => "edit" }
        format.json { render :json => @thing.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /things/1
  # DELETE /things/1.json
  def destroy
    @thing = Thing.find(params[:id])
    @thing.destroy

    respond_to do |format|
      format.html { redirect_to things_url }
      format.json { head :ok }
    end
  end
end
