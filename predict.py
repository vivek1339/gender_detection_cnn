from keras.models import load_model
import cv2
import numpy as np
from keras.optimizers import SGD
model = load_model('Model.h5')
model.compile(SGD(momentum=.9, nesterov=True), 'binary_crossentropy', metrics=['accuracy'])
from keras.preprocessing import image

test_image = image.load_img('women_3.jpg', target_size = (100, 100)) 
test_image = image.img_to_array(test_image)
test_image = np.expand_dims(test_image, axis = 0)
print(test_image.shape)
test_image=test_image/127.5 -1
#predict the result
result = model.predict(test_image)
print(result)