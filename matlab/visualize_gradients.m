function [] = visualize_gradients (f_abcd, scale, exponent, palette) 

syms a b c d x y z

if (~exist('scale','var'))
	scale = 2;
end
if (~exist('exponent','var'))
	exponent = 1/2;
end

Z = [
+1  -1  -1 +1;
+1 +1  -1 -1;
+1  -1 +1  -1;
+1 +1 +1 +1;
];

inv_Z = inv(Z);
abcd_xyz1 = inv_Z*[x;y;z;1];

a_xyz = abcd_xyz1(1);	
b_xyz = abcd_xyz1(2);
c_xyz = abcd_xyz1(3);
d_xyz = abcd_xyz1(4);

funkcja_xyz = subs(f_abcd, {a,b,c,d}, {a_xyz,b_xyz,c_xyz,d_xyz});

g_funkcja_xyz = [diff(funkcja_xyz,x) diff(funkcja_xyz,y) diff(funkcja_xyz,z)];

X_abcd = load('X64_one.txt');
X_abcd = X_abcd/sum(X_abcd(1,:));

idX_abcd_51 = find( abs(0.875 - (X_abcd(:,1) + X_abcd(:,3)) ./ (X_abcd(:,1) + X_abcd(:,2) + X_abcd(:,3) + X_abcd(:,4))) <= 0.0125 );
idX_abcd_12 = find( abs(0.5 - (X_abcd(:,1) + X_abcd(:,3)) ./ (X_abcd(:,1) + X_abcd(:,2) + X_abcd(:,3) + X_abcd(:,4))) <= 0.0125 );
idX_abcd_15 = find( abs(0.125 - (X_abcd(:,1) + X_abcd(:,3)) ./ (X_abcd(:,1) + X_abcd(:,2) + X_abcd(:,3) + X_abcd(:,4))) <= 0.0125 );
%idX_abcd = (1:size(X_abcd,1))';
X_abcd = X_abcd([idX_abcd_51; idX_abcd_12; idX_abcd_15],:);

W = Z(1:3,:);

X_xyz = (W*(X_abcd)')';
X_x = X_xyz(:,1);
X_y = X_xyz(:,2);
X_z = X_xyz(:,3);

f_x = @(x,y,z)(eval(g_funkcja_xyz(1)).*ones(size(x,1),1));
f_y = @(x,y,z)(eval(g_funkcja_xyz(2)).*ones(size(y,1),1));
f_z = @(x,y,z)(eval(g_funkcja_xyz(3)).*ones(size(z,1),1));

d_x = f_x(X_x,X_y,X_z);
d_y = f_y(X_x,X_y,X_z);
d_z = f_z(X_x,X_y,X_z);

d_x = sign(d_x).*abs(d_x).^exponent;
d_y = sign(d_y).*abs(d_y).^exponent;
d_z = sign(d_z).*abs(d_z).^exponent;

tmp = del_infs_nans([X_x X_y X_z d_x d_y d_z]);
X_x = tmp(:,1);
X_y = tmp(:,2);
X_z = tmp(:,3);
d_x = tmp(:,4);
d_y = tmp(:,5);
d_z = tmp(:,6);

midpoint = round(size(palette, 1) / 2);

q = quiver3(X_x, X_y, X_z, d_x, d_y, d_z, scale);

if any(d_z < 0) || any(d_z > 0)
    if ~any(d_z < 0)
        palette = palette((midpoint+1):size(palette, 1), :);
    elseif  ~any(d_z > 0)
        palette = palette(1:(midpoint-1), :);
    else
        palette = palette;
    end
else
    palette = palette(midpoint, :);
end

[~, ~, ind] = histcounts(d_z, size(palette, 1));
cmap = uint8(ind2rgb(ind(:), palette) * 255);
cmap(:,:,4) = 255;
cmap = permute(repmat(cmap, [1 3 1]), [2 1 3]);

set(q.Head, ...
    'ColorBinding', 'interpolated', ...
    'ColorData', reshape(cmap(1:3,:,:), [], 4).');

set(q.Tail, ...
    'ColorBinding', 'interpolated', ...
    'ColorData', reshape(cmap(1:2,:,:), [], 4).');

hold on;
show_tetrahedron;
acbd_gradient_view(gcf,12,1);
hold off;

%view(-90,0)
view(-5,20)
