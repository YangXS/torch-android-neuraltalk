----------------------------------------------------------------------
-- This script is based on file from below address.
--
-- https://github.com/soumith/torch-android/blob/master/demos/android-demo-cifar/application/assets/init-only.lua
-- 
-- The file has been modified to load and run the models required for image captioning.

----------------------------------------------------------------------

require 'torchandroid'
require 'torch'
require 'nn'
require 'nnx'
require 'dok'
require 'image'
require 'nngraph'

local utils = require 'misc.utils'
require 'misc.LanguageModel'
local net_utils = require 'misc.net_utils'

-- fix seed
torch.manualSeed(1)
-- set number of threads
torch.setnumthreads(2)
torch.setdefaulttensortype('torch.DoubleTensor')

-- vocabulary
vocab = torch.load('vocabCPU.t7',  'apkbinary64')
-- cnn network
cnn = torch.load('cnnCPU.t7',  'apkbinary64')
-- language model
lm = torch.load('lmCPU.t7',  'apkbinary64')
print("done with loading models.")


-- function for test image
function getImageCaption(sampledata, width, height)
  local data = torch.ByteTensor(1, 3, 256, 256)
  sampledata = sampledata:reshape(3, height, width)
  data[1] = image.scale(sampledata, 256, 256)
  data = net_utils.prepro(data, false, false)
  local feats = cnn:forward(data)
  local seq = lm:sample(feats, sample_opts)
  local sents = net_utils.decode_sequence(vocab, seq)
  return sents[1]
end
