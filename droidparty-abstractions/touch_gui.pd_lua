local class = pd.Class:new():register("touch_gui")

-- rendering speed (slows down rendering by the given factor)
-- In most cases it should be fine to just set this to 1 to run at full speed,
-- if you have a modern high-speed CPU and GPU. But we use a larger value as
-- default here to deal with low frame rates on some systems. You may have to
-- increase the value even further when running on low-spec systems like the
-- Raspberry Pi.
local R = 3

function class:initialize(sel, atoms)
	--self.inlets = 1
	self.name = "touch"
	self.W = 200
	self.H = 200
	self.label = nil
	self.fontsize = 12
	if(atoms) then
		if(atoms[1]) then self.W = atoms[1] end
		if(atoms[2]) then self.H = atoms[2] end
		if(atoms[3]) then self.name = atoms[3] end
	end
	self.snd = self.name.."-snd"
	self.rcv = self.name.."-rcv"
	self:set_size(self.W, self.H)
	self.bgcolor = {200, 200, 200}
	return true
end

function class:postinitialize()
	self.receiver = pd.Receive:new():register(self, self.rcv, "receive")
	--pd.post("receive name: "..self.rcv)
end

function class:receive(sel, atoms)
	local func = "in_1_"..sel
	if(class[func] ~= nil) then
		self[func](self, atoms[1], atoms[2], atoms[3])
	else pd.post("warning: "..func.." is not a function")
	end
end

function class:in_1_label(label)
	self.label = label
	self:repaint()
end

function class:in_1_fontsize(fontsize)
	self.fontsize = fontsize
	self:repaint()
end

function class:in_1_bgcolor(r, g, b)
	--pd.post("col: "..r.." "..g.." "..b)
	self.bgcolor = {r, g, b};
	self:repaint()
end

function math.clamp(val, lower, upper)
	if lower > upper then lower, upper = upper, lower end -- swap if boundaries supplied the wrong way
	return math.max(lower, math.min(upper, val))
end

function class:mouse_down(x, y, id)
	pd.send(self.snd, "down", {x / self.W, y / self.H})
end

function class:mouse_up(x, y, id)
	pd.send(self.snd, "up", {x, y})
end

function class:mouse_drag(x, y, id)
	pd.send(self.snd, "drag", {x / self.W, y / self.H})
end

function class:paint(g)
	g:set_color(self.bgcolor[1], self.bgcolor[2], self.bgcolor[3])
	-- if(1 or self.dopaint) then g:fill_all() end
	--g:fill_rect(0, 15, self.W, self.H - 15);
	--g:fill_all()
	if(self.label ~= nil) then
		g:set_color(0, 0, 0)
		g:draw_text(self.label, 3, 3, self.W, self.fontsize)
	end
end

